# Browser

This is a bare bones web browser written in Java that supports enough HTML and CSS to render basic webpages.


### Features
- HTML and CSS parsing into DOM and CSSOM.
- Multi-threaded webpage processing.
- Multiple tabs.

### Notable Omissions
- No JavaScript interpreter. Many modern web applications don't render anything interesting because of this. 
- HTML tables are not implemented. This would be nice to add given their prevalence across webpages, but they are not implemented currently.
- There is a large subset of CSS missing from this implementation. For the most part, rules that were not defined are just ignored and replaced with default values.
- Broken HTML support. Many webpages I tested this on contained subtle cases where the HTML was not written correctly. Actual browsers are able to detect this and handle the invalid syntax accordingly.

### Usage
```
    > java -jar browser.jar
```