@echo off

rem comfile
javac -encoding UTF-8 SortingTest.java


echo "-execute your program-"

echo Start Measure %Time%

rem make output
for /l %%i in (1, 1, 11) do (
	java SortingTest < experiment\%%i.txt > experiment\result_%%i.txt
)

echo Stop Measure %Time% 


pause