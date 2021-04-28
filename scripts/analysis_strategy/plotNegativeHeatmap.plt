set encoding iso_8859_1

set terminal unknown
plot ARG1 u 1:2:3 with image

set terminal pdf size 10,5

print "Plotting File: ", ARG1

set out ARG2
unset key

set cbrange [0:100]

set xlabel 'VMs'

# english
set cblabel 'F1 Score'
set ylabel 'Iterations'

# german
#set ylabel 'Iterationen'
#set cblabel 'F1-Ma{\337}'

#set yrange [0:50000]

set title 'Test Result'
set colorbox
#unset ylabel

set xr [GPVAL_DATA_X_MIN:GPVAL_DATA_X_MAX]
set yr [GPVAL_DATA_Y_MIN:GPVAL_DATA_Y_MAX]

plot ARG1 u 1:2:3 with image

unset output

