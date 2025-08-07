Chat : https://chatgpt.com/c/6892763f-a2ac-832d-aa8a-6ea60fabedd7

Added
1. gradle.properties - To overcome warnings

   WARNING: A restricted method in java.lang.System has been called
   WARNING: java.lang.System::load has been called by net.rubygrapefruit.platform.internal.NativeLibraryLoader in an unnamed module (file:/Users/laxmankishorek/.gradle/wrapper/dists/gradle-8.10-bin/deqhafrv1ntovfmgh0nh3npr9/gradle-8.10/lib/native-platform-0.22-milestone-26.jar)
   WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
   WARNING: Restricted methods will be blocked in a future release unless native access is enabled

"org.gradle.jvmargs=--enable-native-access=ALL-UNNAMED"


/usr/libexec/java_home -V     # List all installed JDKs
export JAVA_HOME=export JAVA_HOME="/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home"
echo $JAVA_HOME                # Confirm path
java -version 



Steps to run : 

To build 
"./gradlew clean build" - This generates jar in build/libs/

| Action          | Command                                                                 |
| --------------- |-------------------------------------------------------------------------|
| Start App       | `./gradlew bootRun`                                                     |
| Stop App        | `Ctrl + C`  or `lsof -i :8080` to get PID and kill PID(`kill -9 'pid'`) |
| Clean & Rebuild | `./gradlew clean build`                                                 |


Created project locally

Adding to git hub
   Created an empty repo `GenAIRAGChatbot`

   Add it as remote repo

   command: git remote add origin https://github.com/laxmankishore/GenAIRAGChatbot.git


Deploying workflow after adding secret keys in Git repo

`git add .`

`git commit -m "Adding deploy workflow"`

`git push origin main`


Error:

`I/O error for image [registry-1.docker.io/library/eclipse-temurin]:
org.apache.http.ConnectionClosedException
Premature end of Content-Length delimited message body (expected: 29,721,175; received: 28,268,994)
`

✅ Yes. Jib uses eclipse-temurin (formerly AdoptOpenJDK) as the default base image — unless you explicitly override it.
