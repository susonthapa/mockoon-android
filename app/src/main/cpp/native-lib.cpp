#include <jni.h>
#include <string>
#include <cstdlib>
#include "libnode/node/node.h"

// node's libUV requires all arguments being on contiguous memory
extern "C" JNIEXPORT jint JNICALL
Java_np_com_susanthapa_mockoonandroid_MainActivity_startNodeWithArguments(
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

    // start node with arguments
    int node_result = node::Start(argumentCount, nodeArgs);
    free(argsBuffer);

    return jint(node_result);
}