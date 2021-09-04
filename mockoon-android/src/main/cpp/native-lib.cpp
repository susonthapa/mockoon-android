#include <jni.h>
#include <string>
#include <cstdlib>
#include <unistd.h>
#include <pthread.h>
#include <android/log.h>
#include "libnode/node/node.h"

// start threads to redirect stdout and stderr to logcat
int pipe_stdout[2];
int pipe_stderr[2];
pthread_t threadStdout;
pthread_t threadStderr;
const char *ADBTAG = "Mockoon";

void *thread_stderr_func(void *) {
    ssize_t redirectSize;
    char buf[2048];
    while ((redirectSize = read(pipe_stderr[0], buf, sizeof buf - 1)) > 0) {
        // android log will add a new line anyway
        if (buf[redirectSize - 1] == '\n') {
            --redirectSize;
        }
        buf[redirectSize] = 0;
        __android_log_write(ANDROID_LOG_ERROR, ADBTAG, buf);
    }
}

void *thread_stdout_func(void *) {
    ssize_t redirectSize;
    char buf[2048];
    while ((redirectSize = read(pipe_stdout[0], buf, sizeof buf - 1)) > 0) {
        if (buf[redirectSize - 1] == '\n') {
            --redirectSize;
        }
        buf[redirectSize] = 0;
        __android_log_write(ANDROID_LOG_INFO, ADBTAG, buf);
    }
}

int start_redirecting_stdout_stderr() {
    // set stdout as unbuffered
    setvbuf(stdout, 0, _IONBF, 0);
    pipe(pipe_stdout);
    dup2(pipe_stdout[1], STDOUT_FILENO);

    setvbuf(stderr, 0, _IONBF, 0);
    pipe(pipe_stderr);
    dup2(pipe_stderr[1], STDERR_FILENO);

    if (pthread_create(&threadStdout, 0, thread_stdout_func, 0) == -1) {
        return -1;
    }
    pthread_detach(threadStdout);

    if (pthread_create(&threadStderr, 0, thread_stderr_func, 0) == -1) {
        return -1;
    }
    pthread_detach(threadStderr);

    return 0;
}

// node's libUV requires all arguments being on contiguous memory
extern "C" JNIEXPORT jint JNICALL
Java_np_com_susanthapa_mockoon_1android_MockoonAndroid_startNodeWithArguments(
        JNIEnv *env,
        jobject /* this */,
        jobjectArray arguments) {
    // argc
    jsize argumentCount = env->GetArrayLength(arguments);

    // compute byte size needed for all arguments in contiguous memory
    int cArgumentSize = 0;
    for (int i = 0; i < argumentCount; i++) {
        cArgumentSize += strlen(
                env->GetStringUTFChars((jstring) env->GetObjectArrayElement(arguments, i), 0));
        cArgumentSize++; // for '\0'
    }

    // buffer to hold all arguments in contiguous memory
    char *argsBuffer = (char *) calloc(cArgumentSize, sizeof(char));

    // arguments to pass to node js runtime
    char *nodeArgs[argumentCount];

    // to iterate through the expected start position of each argument in argsBuffer
    char *currentArgsPosition = argsBuffer;

    // populate the argsBuffer and nodeArgs
    for (int i = 0; i < argumentCount; i++) {
        const char *currentArgument = env->GetStringUTFChars(
                (jstring) env->GetObjectArrayElement(arguments, i), 0);
        // copy current argument to its expected position in argsBuffer
        strncpy(currentArgsPosition, currentArgument, strlen(currentArgument));

        // save current argument start position in nodeArgs
        nodeArgs[i] = currentArgsPosition;

        // increment to the next argument's expected position
        currentArgsPosition += strlen(currentArgsPosition) + 1;
    }

    // start redirection
    if (start_redirecting_stdout_stderr() == -1) {
        __android_log_write(ANDROID_LOG_ERROR, ADBTAG, "Couldn't start redirecting stdout and stderr to logcat!");
    }
    // start node with arguments and catch any error
    int node_result = -1;
    try {
        node_result = node::Start(argumentCount, nodeArgs);
    } catch (...) {
        __android_log_write(ANDROID_LOG_ERROR, ADBTAG, "Failed to start node js engine!");
    }

    return jint(node_result);
}