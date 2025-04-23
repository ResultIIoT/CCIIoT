# CCDetector-Update is a co-modified code clone detection tool that is a modification of the original CCDetector (a NiCad-based tool for all projects that enables clone type separation and co-modified clone detection).

## Prerequisites

1. [NiCad-6.2](https://www.txl.ca/txl-nicaddownload.html)

2. Java17

3. Maven3.8

4. Python3.8

## Clone Type Separation

This process is implemented by  *CloneSeparator.java*.

- Input : *InputCS*. If you don't have this folder, you need to create it by yourself and then, put the NiCad result files in this folder.

- Output : *InputCS/Results/“......clone-abstract”*  (If it is not in the Result folder, manually move it into the Result folder under the "......functions clones" folder. )

## Co-changed Clone Detection

This process is implemented by *FindCoChangeClone.java*.

- Input  : *InputCC*. If you don't have this folder, you need to create it by yourself and then, put in a different version of NiCad for the item to be tested. If you have multiple projects, each project needs a separate folder.

- Output : *outputCC/......*.

## Co-changed Clone Type Separation

This process is implemented by *CCloneSeparator.java*.

- Input 1 : *outputCC*. If you don't have this folder, you need to create it by yourself and then, put the *Co-changed Clone Detection* result files in this folder.

- Input 2 : *InputCS/Results*. If you don't have this folder, you need to create it by yourself and then, put the *Clone Type Separation* result files in this folder.

- Output: The result is written into the *FinalResults* file in the *Co-changed Clone Detection* result files.
