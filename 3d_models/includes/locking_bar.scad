/*
	Bar of the locking mechanism.
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/


// Bar that is going to be moved by the cylinder.
module locking_bar()
{
	difference()
	{
		cube([70, 20, 20]);
		translate([20, 11.1, 4.5])
		cube([8.1, 9.1, 11]);
		translate([35.1, 11.1, 4.5])
		cube([8.1, 9.1, 11]);
		
		translate([35.1, 19.1, 4.5])
		rotate(25, [0, 0, 1])
		cube([5, 5, 11]);


		translate([20.0, 19.1, 4.5])
		rotate(25, [0, 0, 1])
		cube([5, 5, 11]);

		translate([22.6, 19.1, 4.5])
		rotate(-25, [0, 0, 1])
		cube([5, 5, 11]);

		translate([37.7, 19.1, 4.5])
		rotate(-25, [0, 0, 1])
		cube([5, 5, 11]);
	}
}