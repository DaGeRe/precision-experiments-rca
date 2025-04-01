set encoding iso_8859_1
set terminal pdf size 4,6

set out 'requiredVMs.pdf'

set xlabel 'Change in %'
set ylabel 'Required VMs'

set multiplot layout 2,1

set key top left

set title 'Instrumentation'
plot "instrumentation_2.csv" using 2:3 w linespoint linewidth 2 title "Depth=2", \
	"instrumentation_4.csv" using 2:3 w linespoint linewidth 2 title "Depth=4", \
	"instrumentation_6.csv" using 2:3 w linespoint linewidth 2 title "Depth=6", \
	"instrumentation_8.csv" using 2:3 w linespoint linewidth 2 title "Depth=8"

set title 'Sampling
set key top left
plot "sampling_2.csv" using 2:3 w linespoint linewidth 2 title "Depth=2", \
	"sampling_4.csv" using 2:3 w linespoint linewidth 2 title "Depth=4", \
	"sampling_6.csv" using 2:3 w linespoint linewidth 2 title "Depth=6", \
	"sampling_8.csv" using 2:3 w linespoint linewidth 2 title "Depth=8"
	

unset multiplot
unset output

