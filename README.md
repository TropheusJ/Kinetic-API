# Create - Refabricated

## About

This project intends to port the Create mod to Fabric. All help is welcome, this is not going to be easy.
The entirety(?) of Create has already been remapped from MCP to Yarn, and other important files have been moved too. now it's time to fix everything else. 

## Contributing
Cut (yes, cut. not copy. Gotta keep track of what's done.) and paste a file or set of files from "oh god dont open this folder" into src. Fix errors and PR.
Try to keep your code and organization consistent with the original mod. See Development Roadmap for why this might be difficult. Please comment everything that isn't self-explanatory.
note: build.gradle.old and gradle.properties.old are the files from the original mod unchanged. 

## Development Roadmap 
The regular mod is going to be split into multiple for this port. This repo is going to be for the rotation and contraption API/engine/whatever it is. I'm starting with the basics. literally. Stuff from create.content.contraptions.base needs to come first, everything relies on that.