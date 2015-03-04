/*
	Model of the single cell battery holder.
	NOT TO BE PRINTED!
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/


// Single cell battery holder
module battery_single()
{
	difference()
	{
		cube([16, 58, 15]);
		translate([8, 29, -3])
		cylinder(d=3, h=20, $fs = 1);
	}
}

