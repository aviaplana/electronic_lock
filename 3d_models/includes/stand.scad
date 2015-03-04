/*
	Stand for the cylinder and casing. 
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/

include <./stand_top.scad>;
include <./stand_bottom.scad>;


module lock_stand()
{
	lock_stand_top();
	lock_stand_bottom();
}