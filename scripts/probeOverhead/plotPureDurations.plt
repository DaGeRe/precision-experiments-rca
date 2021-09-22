set encoding iso_8859_1
set terminal pdf size 8,5

set out 'pure_operationExecution.pdf'

set title 'Methodenausführungszeiten (OperationExecutionRecord)'

set xlabel 'Ebenenanzahl'
set ylabel 'Dauer in {/Symbol m}s'

set key left top 
	
plot 'pure_durations.csv' u 1:2 w linespoint lc "red" title 'Mit AspectJ Instrumentierung', \
	'pure_durations.csv' u 1:($2-$3):($2+$3) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'pure_durations.csv' u 1:4 w linespoint lc "yellow" title 'Mit Quelltextinstrumentierung', \
	'pure_durations.csv' u 1:($4-$5):($4+$5) w filledcurves lc "yellow" notitle fs transparent solid 0.5, \
     'pure_durations.csv' u 1:6 w linespoint lc "green" title 'Mit Quelltextinstrumentierung und CircularFifoQueue', \
	'pure_durations.csv' u 1:($6-$7):($6+$7) w filledcurves lc "green" notitle fs transparent solid 0.5 

	
unset output

set terminal pdf size 8,10

set out 'pure_reducedOperationExecution.pdf'

set multiplot layout 2,1

set title 'Methodenausführungszeiten (ReducedOperationExecutionRecord)'

set xlabel 'Ebenenanzahl'
set ylabel 'Dauer in {/Symbol m}s'

set key left top 
	
plot 'pure_durations.csv' u 1:8 w linespoint lc "red" title 'Mit AspectJ Instrumentierung', \
	'pure_durations.csv' u 1:($8-$9):($8+$9) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'pure_durations.csv' u 1:10 w linespoint lc "yellow" title 'Mit Quelltextinstrumentierung', \
	'pure_durations.csv' u 1:($10-$11):($12+$11) w filledcurves lc "yellow" notitle fs transparent solid 0.5, \
     'pure_durations.csv' u 1:12 w linespoint lc "green" title 'Mit Quelltextinstrumentierung und CircularFifoQueue', \
	'pure_durations.csv' u 1:($12-$13):($12+$13) w filledcurves lc "green" notitle fs transparent solid 0.5
	
plot 'pure_durations.csv' u 1:8 w linespoint lc "red" title 'Mit AspectJ Instrumentierung', \
	'pure_durations.csv' u 1:($8-$9):($8+$9) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'pure_durations.csv' u 1:10 w linespoint lc "yellow" title 'Mit Quelltextinstrumentierung', \
	'pure_durations.csv' u 1:($10-$11):($10+$11) w filledcurves lc "yellow" notitle fs transparent solid 0.5, \
     'pure_durations.csv' u 1:14 w linespoint lc "green" title 'Mit Quelltextinstrumentierung, CircularFifoQueue und selektiver Instrumentierung', \
	'pure_durations.csv' u 1:($14-$15):($14+$15) w filledcurves lc "green" notitle fs transparent solid 0.5
	
unset multiplot
unset output


