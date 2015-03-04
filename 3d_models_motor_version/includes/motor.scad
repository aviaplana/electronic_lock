/*
	Motor with the join. This motor is used to move the gear.
	NOT TO BE PRINTED!
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/

// Motor with join
module motor()
{
	cylinder(d=24, h=30.5);

	translate([0,0,-10.2])
	cylinder(h=10.4, d=2);

	translate([0,0,-12.2])
	cylinder(d=17.2, h=2);
}
