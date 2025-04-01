set encoding iso_8859_1
set terminal pdf size 7,4

set out 'overhead.pdf'

set xlabel 'Call Tree Depth'
set ylabel 'Duration / ms'

set key left top 
	
plot 'instrumentation.csv' u 1:3 w linespoint lc "red" title 'Instrumentation (Complete)', \
	'instrumentation.csv' u 1:($3-$2):($3+$2) w filledcurves lc "red" notitle fs transparent solid 0.5, \
     'sampling.csv' u 1:3w linespoint lc "blue" title 'Sampling', \
	'sampling.csv' u 1:($3-$2):($3+$2) w filledcurves lc "blue" notitle fs transparent solid 0.5
	
unset output
