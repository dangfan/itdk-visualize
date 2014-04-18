itdk-visualize
==============

Visualize the AS topology using data from ITDK.

Requirement
-----------
JDK 1.7
gephi-toolkit.jar
Python 2.7

Instruction
-----------

1. Use program in `itdk2gexf` to convert ITDK txt dataset to `gexf` formats. You may need to change the directory in the source file.
2. Use Java program in `gexf2images` to convert `gexf` files to `.png` images. You may also need to change the directory in `Main.java`. Remember to use the library `gephi-toolkit.jar` and `org-gephi-plugins-layout-geo.jar` (which is provided in this reposity).