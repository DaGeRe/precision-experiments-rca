set encoding iso_8859_1
set terminal pdf size 7,4

set out 'pure_operationExecution.pdf'

set title 'Methodenausführungszeiten (OperationExecutionRecord)'

set xlabel 'Ebenenanzahl'
set ylabel 'Dauer in {/Symbol m}s'

set key at graph 0.65,0.975
	
plot 'pure_durations.csv' u 1:2 w linespoint lc "red" title 'AspectJ Instrumentierung', \
	'pure_durations.csv' u 1:($2-$3):($2+$3) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'pure_durations.csv' u 1:4 w linespoint lc "yellow" title 'Quelltextinstrumentierung', \
	'pure_durations.csv' u 1:($4-$5):($4+$5) w filledcurves lc "yellow" notitle fs transparent solid 0.5, \
     'pure_durations.csv' u 1:6 w linespoint lc "green" title 'Quelltextinstrumentierung und selektive Instrumentierung', \
	'pure_durations.csv' u 1:($6-$7):($6+$7) w filledcurves lc "green" notitle fs transparent solid 0.5 

	
unset output

set terminal pdf size 7,4

set out 'pure_duration.pdf'

set title 'Methodenausführungszeiten (DurationRecord)'

set xlabel 'Ebenenanzahl'
set ylabel 'Dauer in {/Symbol m}s'
	
plot 'pure_durations.csv' u 1:8 w linespoint lc "red" title 'AspectJ Instrumentierung', \
	'pure_durations.csv' u 1:($8-$9):($8+$9) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'pure_durations.csv' u 1:10 w linespoint lc "yellow" title 'Quelltextinstrumentierung', \
	'pure_durations.csv' u 1:($10-$11):($10+$11) w filledcurves lc "yellow" notitle fs transparent solid 0.5, \
     'pure_durations.csv' u 1:12 w linespoint lc "green" title 'Quelltextinstrumentierung und selektive Instrumentierung', \
	'pure_durations.csv' u 1:($12-$13):($12+$13) w filledcurves lc "green" notitle fs transparent solid 0.5, \
     'pure_durations.csv' u 1:14 w linespoint lc "green" title 'Quelltextinstrumentierung, selektive Instrumentierung und Aggregation', \
	'pure_durations.csv' u 1:($14-$15):($14+$15) w filledcurves lc "green" notitle fs transparent solid 0.5
	
unset multiplot
unset output


