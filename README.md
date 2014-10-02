# ServerTools Backup

ServerTools Backup is an addon for ServerTools that gives you the ability to create server backups. These backups can be both on demand by using the `/backup` command, and scheduled automatically at a certain interval.

## Developing
If you forked this repository and want to help contribute, follow the following steps:

1. Clone the repo to a folder on your computer.
2. Open a terminal/command window in the root folder of the repo.
3. Type: `gradlew setupDecompWorkspace` and hit ENTER.

#### Intellij Idea
1. Choose Import Project, and select the `build.gradle` file.
2. Click OK on the gradle project window.

#### Eclipse
1. In the same terminal as before, type: `gradlew eclipse`.
2. Import the repo folder as an eclipse project.

## Building the Mod
If for whatever reason you want to build your own copy of the mod, you can! ServerTools leverages the power of [Gradle](http://gradle.org) to make building easy. Follow the following steps to build your copy of ServerTools:

1. Clone or download this repo to your computer.
2. Open a terminal/command window in the root folder of the repo.
3. Type: `gradlew setupCIWorkspace build` or on Windows: `gradlew.bat setupCIWorkspace build`, and hit ENTER.
4. Your jar will output to `PROJECTDIR/build/libs/`.

## License

```
Copyright 2014 ServerTools Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```