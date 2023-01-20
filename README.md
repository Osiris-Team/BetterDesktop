# BetterDesktop
A better desktop layout for fast app access.

## Installation
1. Make sure you have Java 11 or higher installed
2. Download the [BetterDesktop.jar](https://github.com/Osiris-Team/BetterDesktop/tree/target/BetterDesktop.jar)
and put it in a folder of your choice.
3. Double click the .jar file.

<small>To uninstall just delete the jar, 
to remove all its data delete the whole folder (note that your original desktop is saved there).</small>


## Todo
![example_layout](https://user-images.githubusercontent.com/59899645/212469912-bd92e675-2a63-47b9-b63c-46cbbadf7b7c.png)
- The "All" tab contains a scrollable list of all installed programs, sorted alphabetically.
- The "Recent" tab contains a list sorted by last opened program first.
- The "Favorites" tab contains a list sorted by most used program first.
- Left-clicking on a program icon should open it. Right-clicking could be harder to implement, so let's skip that for now, but it should do something similar to the regular right-click, or just open its location in the file explorer.

There are some extras that are not included in the picture above:
- Each tab could also have a search bar below its title to search for programs by their names.
- The titles can be smaller, as well as the gaps between the tabs, messed it up a bit there. The titles can also be removed entirely and replaced by the search fields tooltip, like "Search in favorites..." or "Search in all...".
- The sizes for the tabs should be calculated on init and based on the screen size ( - task bar).
- The "Recent" and "Favorites" tabs could additionally contain links to websites (their icons) that open that website in the browser when clicked. They could also contain files, that when clicked open them.
- If the above is implemented it would also make sense to add program/file names below their icons, to differentiate files of the same type.

This won't be possible through Rainmeter, because it bumps up my CPU usage to 20% and has like 2fps when scrolling.

Development build currently looks like this:
![image](https://user-images.githubusercontent.com/59899645/212994843-039dd25f-458e-4ef6-a03d-41378968f582.png)

- `/User/Desktop` and `/Public/Desktop` contents will be moved into the BetterDesktop program directory
and added to the favorites tab, thus clearing up the background.
- `/Program Files` and `/Program Files (x86)` and `/Start Menu/Programs` 
on all drives will be scanned for .exe and .lnk files, and then added to the all tab.



