set encoding iso_8859_1
set terminal pdf size 10,5

print "Plotting File: ", ARG1

set out 'resultTemp.pdf'

set cbrange [0:100]

set xrange [0:100]
set xlabel 'VMs'
set ylabel 'Iterations'
#set cblabel 'F1-Ma{\337}'
set cblabel 'F_1 Score'
set yrange [0:50]

# set title 'Mann-Whitney-Test'
set colorbox
unset ylabel
plot ARG1 u ($1-5):($2-5):3 with image

unset output

