/*
	H-Lock casing and stand.
	The gears are made using the MCAD library ( https://github.com/openscad/MCAD ).
	The yellow part ar not meant to be printed.
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/

include <./includes/case.scad>
include <./includes/stand.scad>
include <./includes/electronics.scad>
include <./includes/locking_bar.scad>
include <./includes/cylinder.scad>
include <./includes/gears.scad>



color([.8, .2, .2])
gears();


color([.4, .4, .8])
translate([-40, -30, -43])
locking_bar();


color([.4, .8, .4])
translate([-9, 6.5, -48.1])
lock_stand();


color([1,1,0])
{
	// NOT TO BE PRINTED.
	translate([0, 19.5, -63.1])
	cylinder_lock();

	// NOT TO BE PRINTED.
	electronics();
}


color([.5, .5, .5, .3])	
case();

