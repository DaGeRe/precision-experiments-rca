set encoding iso_8859_1

set terminal pdf size 11,3
set out 'rca_sizeEvolution_de.pdf'

set multiplot layout 1,3

set cbrange [0:0.1]

set xlabel 'Baumtiefe'
set ylabel 'Analysierte Knoten'
set cblabel 'Relative Standardabweichung'

set title 'ADD'
plot 'deviations-ADD.csv' u 1:4:3 with image title ''

set title 'RAM'
plot 'deviations-RAM.csv' u 1:4:3 with image title ''

set title 'SYSOUT'
plot 'deviations-SYSOUT.csv' u 1:4:3 with image title ''

unset multiplot
unset output
