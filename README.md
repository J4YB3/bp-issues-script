# bp-issues-script
A simple formatting script written in Java to convert the Issues of our "Bachelor Praktikum", that are returned by the GitLab API in JSON Format, to .tex files.
  
For Time spent following formats are possible: \d+h \d+m, \d+h, \d+m  
The velocity is calculated by the script according to the Story Points and the Time spent  
For the sprint field the last number is used. When Sprint is "xx" this field will be set to "-" inside the final document  
Also when the time spent contains "x" the velocity is set to 0.0 and the time spent is set to 0h

Other than that the format of the issue description must match the following:

#### Description
Multiple lines of description are possible. To separate paragraphs two newline characters are needed.

Like so. 

Enumerations are possible like:
* This one
* And this one that shows, that commas can be read and even <del>strikeout</del> text can be converted. However it must be used with the html tags \<del> and \</del> inside the issue

The description can be as long as you like. Linking to other Issues by ID reference like #458 is also possible and will be converted to a \hyperref inside LaTeX.

#### Tasks
* [ ] The tasks of the issue
* [x] Even though these are not contained in the final .tex file
* [ ] The section is needed in order for the script to work. But can be empty

#### Acceptance Criteria
* Enumerations are recognized, as well as
* [ ] Task lists
* [ ] And even sub-enumerations can be converted correctly
  * like this one
  * or this
* [x] And returning to normal enumeration is possible after that
* [ ] <del>strikeout</del> text is also supported inside the acceptance criteria

#### Points 
|  |  |
| ----- | ----- |
| Story points | 8 |
| Time spent | 7h 15m |
| Velocity (SP/h) | 1.103 |
| Sprint | 05, 06, 07 |

#### Notes
* These are the notes for the issue
* When the notes don't contain anything it leads to an error. At least a single * must come after the '#### Notes' line
* /estimate, \estimate, /spend, \spend, and empty Notes such as * "" are ignored
