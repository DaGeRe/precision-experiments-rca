set encoding iso_8859_1
set terminal pdf size 8,5

set out 'outputPDFs/probes_ASE.pdf'

set title 'Iteration Measurement Duration'

set xlabel 'Level Count'
set ylabel 'Duration in ms'

set key left top
	
set xrange [0:64]

plot 'outputCSVs/measurementDurations.csv' u 1:4 w linespoint lc "red" title 'AspectJ Instrumentation', \
	'outputCSVs/measurementDurations.csv' u 1:($4*(1-$5)):($4*(1+$5)) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'outputCSVs/measurementDurations.csv' u 1:6 w linespoint lc "violet" title 'Code Instrumentation', \
	'outputCSVs/measurementDurations.csv' u 1:($6*(1-$7)):($6*(1+$7)) w filledcurves lc "violet" notitle fs transparent solid 0.5, \
     'outputCSVs/measurementDurations.csv' u 1:8 w linespoint lc "orange" title '... and CircularFifoQueue', \
	'outputCSVs/measurementDurations.csv' u 1:($8*(1-$9)):($8*(1+$9)) w filledcurves lc "orange" notitle fs transparent solid 0.5 , \
     'outputCSVs/measurementDurations.csv' u 1:10 w linespoint lc "yellow" title '... and DurationRecord', \
	'outputCSVs/measurementDurations.csv' u 1:($10*(1-$9)):($10*(1+$9)) w filledcurves lc "yellow" notitle fs transparent solid 0.5 , \
     'outputCSVs/measurementDurations.csv' u 1:12 w linespoint lc "green" title '... and Aggregation', \
	'outputCSVs/measurementDurations.csv' u 1:($12*(1-$11)):($12*(1+$11)) w filledcurves lc "green" notitle fs transparent solid 0.5 , \
     'outputCSVs/measurementDurations.csv' u 1:2 w linespoint lc "blue" title 'No Instrumentation', \
	'outputCSVs/measurementDurations.csv' u 1:($2*(1-$3)):($2*(1+$3)) w filledcurves lc "blue" notitle fs transparent solid 0.5, \
     'opentelemetry.csv' u 1:2 w linespoint lc "#a83655" title 'OpenTelemetry', \
	'opentelemetry.csv' u 1:($2*(1-$3)):($2*(1+$3)) w filledcurves lc "#a83655" notitle fs transparent solid 0.5

	
unset output

