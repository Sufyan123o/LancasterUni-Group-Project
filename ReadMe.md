# Compilation Instructions
- maven needs to be installed.
then:
```
cd pixel-editor

mvn clean compile

mvn exec:java
```

and the program should run.

# Git Instructions
- All contributions should be done through a merge request from another branch. (this can be done within gitlab)
- The request must pass all tests.
- A branch must be made for each feature developed.


Top tips for git:

This creates a new branch. (replace new-branch with whatever you wish to call it)
```
git branch new-branch
```

This switches from your current branch.
```
git checkout new-branch
```

To add files to be commited 
```
git add *

or

git add [insert file]
```

To commit (this is local)
```
git commit -m "insert commit message"
```

To push it to the repo
```
git push
```

The changes will be stored in a new branch in the repo
open gitlab and click merge branch.

then locally
```
git checkout main

git pull
```

this will make sure the merge is on your own machine and removes the old branch.
