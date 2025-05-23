set encoding iso_8859_1
set terminal pdf size 6,3

set out 'overhead.pdf'

set xlabel 'Call Tree Depth'
set ylabel 'Duration / ms'

set key left top 

set logscale y
	
plot 'instrumentation-usc.csv' u 1:3 w linespoint lc "red" title 'Instrumentation (USC)', \
	'instrumentation-usc.csv' u 1:($3-$2):($3+$2) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'instrumentation-complete.csv' u 1:3 w linespoint lc "purple" title 'Instrumentation (Complete)', \
	'instrumentation-complete.csv' u 1:($3-$2):($3+$2) w filledcurves lc "purple" notitle fs transparent solid 0.5, \
     'sampling-interval1.csv' u 1:3w linespoint lc "blue" title 'Sampling (1ms)', \
	'sampling-interval1.csv' u 1:($3-$2):($3+$2) w filledcurves lc "blue" notitle fs transparent solid 0.5, \
     'sampling-interval20.csv' u 1:3w linespoint lc "green" title 'Sampling (20ms)', \
	'sampling-interval20.csv' u 1:($3-$2):($3+$2) w filledcurves lc "green" notitle fs transparent solid 0.5
	
unset output

set out 'overhead-small.pdf'

set xlabel 'Call Tree Depth'
set ylabel 'Duration / ms'

set key left top 
	
plot 'instrumentation-usc.csv' u 1:3 w linespoint lc "red" title 'Instrumentation (USC)', \
	'instrumentation-usc.csv' u 1:($3-$2):($3+$2) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'overhead-pure.csv' u 1:3 w linespoint lc "purple" title 'Pure', \
	'overhead-pure.csv' u 1:($3-$2):($3+$2) w filledcurves lc "purple" notitle fs transparent solid 0.5
	
unset output
