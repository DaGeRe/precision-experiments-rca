set encoding iso_8859_1
set terminal pdf size 8,5

set out 'singleNodeTree_operationExecution.pdf'

set title 'Entwicklung der Iterationsmessdauer (OperationExecutionRecord)'

set xlabel 'Ebenenanzahl'
set ylabel 'Dauer in ms'

set key left top 
	
plot 'durations.csv' u 1:4 w linespoint lc "red" title 'Mit AspectJ Instrumentierung', \
	'durations.csv' u 1:($4*(1-$5)):($4*(1+$5)) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'durations.csv' u 1:6 w linespoint lc "yellow" title 'Mit Quelltextinstrumentierung', \
	'durations.csv' u 1:($6*(1-$7)):($6*(1+$7)) w filledcurves lc "yellow" notitle fs transparent solid 0.5, \
     'durations.csv' u 1:8 w linespoint lc "green" title 'Mit Quelltextinstrumentierung und CircularFifoQueue', \
	'durations.csv' u 1:($8*(1-$9)):($8*(1+$9)) w filledcurves lc "green" notitle fs transparent solid 0.5 , \
     'durations.csv' u 1:2 w linespoint lc "blue" title 'Ohne Instrumentierung', \
	'durations.csv' u 1:($2*(1-$3)):($2*(1+$3)) w filledcurves lc "skyblue" notitle fs transparent solid 0.5

	
unset output

set terminal pdf size 8,10

set out 'singleNodeTree_reducedOperationExecution.pdf'

set multiplot layout 2,1

set title 'Entwicklung der Iterationsmessdauer (ReducedOperationExecutionRecord)'

set xlabel 'Ebenenanzahl'
set ylabel 'Dauer in ms'
	
plot 'durations.csv' u 1:10 w linespoint lc "red" title 'Mit AspectJ Instrumentierung', \
	'durations.csv' u 1:($10*(1-$11)):($10*(1+$11)) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'durations.csv' u 1:12 w linespoint lc "yellow" title 'Mit Quelltextinstrumentierung', \
	'durations.csv' u 1:($12*(1-$13)):($12*(1+$13)) w filledcurves lc "yellow" notitle fs transparent solid 0.5, \
     'durations.csv' u 1:14 w linespoint lc "green" title 'Mit Quelltextinstrumentierung und CircularFifoQueue', \
	'durations.csv' u 1:($14*(1-$15)):($14*(1+$15)) w filledcurves lc "green" notitle fs transparent solid 0.5, \
     'durations.csv' u 1:2 w linespoint lc "blue" title 'Ohne Instrumentierung', \
	'durations.csv' u 1:($2*(1-$3)):($2*(1+$3)) w filledcurves lc "skyblue" notitle fs transparent solid 0.5
	
plot 'durations.csv' u 1:10 w linespoint lc "red" title 'Mit AspectJ Instrumentierung', \
	'durations.csv' u 1:($10*(1-$11)):($10*(1+$11)) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'durations.csv' u 1:12 w linespoint lc "yellow" title 'Mit Quelltextinstrumentierung', \
	'durations.csv' u 1:($12*(1-$13)):($12*(1+$13)) w filledcurves lc "yellow" notitle fs transparent solid 0.5, \
     'durations.csv' u 1:16 w linespoint lc "green" title 'Mit Quelltextinstrumentierung, CircularFifoQueue und selektiver Instrumentierung', \
	'durations.csv' u 1:($16*(1-$17)):($16*(1+$17)) w filledcurves lc "green" notitle fs transparent solid 0.5, \
     'durations.csv' u 1:2 w linespoint lc "blue" title 'Ohne Instrumentierung', \
	'durations.csv' u 1:($2*(1-$3)):($2*(1+$3)) w filledcurves lc "skyblue" notitle fs transparent solid 0.5
	
unset multiplot
unset output


