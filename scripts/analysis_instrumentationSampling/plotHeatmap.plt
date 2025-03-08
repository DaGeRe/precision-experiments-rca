set encoding iso_8859_1
set terminal pdf size 10,5

print "Plotting File: ", ARG1

set out 'resultTemp.pdf'

set cbrange [0:100]

set xrange [0:200]
set xlabel 'VMs'
set ylabel 'Iterationen'
#set cblabel 'F1-Ma{\337}'
set cblabel 'F1 Score'
set yrange [0:100]

set title 'T-Test'
set colorbox
unset ylabel
plot ARG1 u 1:2:3 with image

unset output

