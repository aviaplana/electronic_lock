/*
	H-Lock casing, motor version. This is the very first case that we printed. 
	Turned to be that the motor was to fast, so this version was dismissed. 
	We ended up using a servo.
	The gears are made using the MCAD library ( https://github.com/openscad/MCAD ).
	The yellow part ar not meant to be printed.
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/

include <./includes/motor.scad>
include <./includes/battery_holder_3.scad>
include <./includes/battery_holder_single.scad>
include <./includes/case.scad>
include <./includes/gears.scad>
include <./includes/cylinder.scad>

color([.8, .2, .2])
gears();


// NOT TO BE PRINTED.
color([1,1,0])
{
	translate([0, 20, -63])
	cylinder_lock();

	rotate(-90,[1,0,0])
	translate([0,-18.4,62])
	motor();

	translate([18, 45, 11])
	battery_3();

	translate([49, -15, 11])
	battery_single();
}

color([.5, .5, .5, .3])
case();
