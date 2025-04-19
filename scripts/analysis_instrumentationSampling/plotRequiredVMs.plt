set encoding iso_8859_1
set terminal pdf size 4,10

set out 'requiredVMs.pdf'

set xlabel 'Change in %'
set ylabel 'Required VMs'

set multiplot layout 5,1

set key top left

set title 'Instrumentation-Complete'
plot "instrumentation-complete_2.csv" using 2:3 w linespoint linewidth 2 title "Depth=2", \
	"instrumentation-complete_4.csv" using 2:3 w linespoint linewidth 2 title "Depth=4", \
	"instrumentation-complete_6.csv" using 2:3 w linespoint linewidth 2 title "Depth=6", \
	"instrumentation-complete_8.csv" using 2:3 w linespoint linewidth 2 title "Depth=8"

set title 'Instrumentation-USC'
set key top left
plot "instrumentation-usc_2.csv" using 2:3 w linespoint linewidth 2 title "Depth=2", \
	"instrumentation-usc_4.csv" using 2:3 w linespoint linewidth 2 title "Depth=4", \
	"instrumentation-usc_6.csv" using 2:3 w linespoint linewidth 2 title "Depth=6", \
	"instrumentation-usc_8.csv" using 2:3 w linespoint linewidth 2 title "Depth=8"

set title 'Sampling (1ms)'
set key top left
plot "sampling-interval1_2.csv" using 2:3 w linespoint linewidth 2 title "Depth=2", \
	"sampling-interval1_4.csv" using 2:3 w linespoint linewidth 2 title "Depth=4", \
	"sampling-interval1_6.csv" using 2:3 w linespoint linewidth 2 title "Depth=6", \
	"sampling-interval1_8.csv" using 2:3 w linespoint linewidth 2 title "Depth=8"

set title 'Sampling (10ms)'
set key top left
plot "sampling-interval10_2.csv" using 2:3 w linespoint linewidth 2 title "Depth=2", \
	"sampling-interval10_4.csv" using 2:3 w linespoint linewidth 2 title "Depth=4", \
	"sampling-interval10_6.csv" using 2:3 w linespoint linewidth 2 title "Depth=6", \
	"sampling-interval10_8.csv" using 2:3 w linespoint linewidth 2 title "Depth=8"
	
set title 'Sampling (20ms)'
set key top left
plot "sampling-interval20_2.csv" using 2:3 w linespoint linewidth 2 title "Depth=2", \
	"sampling-interval20_4.csv" using 2:3 w linespoint linewidth 2 title "Depth=4", \
	"sampling-interval20_6.csv" using 2:3 w linespoint linewidth 2 title "Depth=6", \
	"sampling-interval20_8.csv" using 2:3 w linespoint linewidth 2 title "Depth=8"
	


unset multiplot
unset output

