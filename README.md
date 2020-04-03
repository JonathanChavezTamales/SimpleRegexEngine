# Regex Engine

Author: Jonathan Chávez

This is a simple regex engine that accepts concatenation, union, kleene star and parenthesis.

Implementing using Shunting Yard algorithm to parse the expression, then Thompson's construction to create a NFA given the parsed expression, finally traversing the NFA.

### Hierarchy:
* `()`
* `*`
* concatenation
* `+`

## How to use

You can run with docker and pass the parameters at runtime with the following instructions:

`git clone https://github.com/JonathanChavezTamales/SimpleRegexEngine && cd SimpleRegexEngine`

`docker build -t regexengine`

`docker run -t regexengine [string] [pattern] [replacement]`

## Reference:

* [https://deniskyashif.com/2019/02/17/implementing-a-regular-expression-engine/](https://deniskyashif.com/2019/02/17/implementing-a-regular-expression-engine/)

* [https://swtch.com/~rsc/regexp/regexp1.html](https://swtch.com/~rsc/regexp/regexp1.html)
