/*
	Model of the 3 cells battery holder.
	NOT TO BE PRINTED!
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/

// 3 Cells battery holder
module battery_3()
{
	difference()
	{
		cube([47, 58, 15]);
		translate([8, 29, -3])
		cylinder(d=3, h=20, $fs = 1);
		translate([39, 29, -3])
		cylinder(d=3, h=20, $fs = 1);
	}
}
