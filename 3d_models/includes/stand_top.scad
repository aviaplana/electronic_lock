/*
	Top part of the stand.
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/

module lock_stand_top()
{
	union()
	{

//START Bottom part
		/*
			This part is designed to fit into the bottom part of the stand. 
			We had to split the stand in 2 part because it was to large for the printer
		*/
		difference()
		{
			translate([-3, 89, -3])
			cube([24, 20, 36]);

			translate([0, 88.9, 0])
			cube([16, 5.2, 28]);

			translate([0, 94, 0])
			cube([18.1, 20.1, 30]);
		
		}

//END Bottom part


// START holding arms
		
		// Center part
		translate([-28.9, 49, 27])
		cube([78.8, 10, 3]);

		// Left arm
		difference()
		{
			translate([-28.9, 49, 27])
			cube([3, 10, 33]);

			translate([-18.9, 52, 50])
			rotate(90, [0, 0, 1])
			hole_case();
		}

		// Right arm
		difference()
		{
			translate([46.9, 49, 27])
			cube([3, 10, 33]);

			translate([56.9, 52, 50])
			rotate(90, [0, 0, 1])
			hole_case();
		}

// END holding arms

		
		difference()
		{
			union()
			{
				// Body bar
				cube([18, 94, 30]);

				// Top cube
				translate([-6, -40, 0])
				cube([30, 40, 30]);
			}
			
			// To make the inside of the body empty
			translate([3, 21, 3])
			cube([12, 83.5, 24]);

			// Top hole for the bar mechanism
			translate([-10, -36.75, 4.75])
			cube([40, 20.5, 20.5]);

			// This is the cylinder hole
			translate([9, 13, -1])
			{
				cylinder(d=10.5, h=32, $fs = 0.5);
				translate([-5.25, -25, 0])
				cube([10.5, 25, 32]);

				translate([0, -19.5, 0])
				cylinder(d=13.5, h=80, $fs = 0.5);

				translate([0, -19.5, 0])
				cylinder(d=17.5, h=70, $fs = 0.5);
			}

			// This part is for the "wing" of the cylinder, the part that is going to move the bar.
			translate([-10, -20.1, 10])
			cube([ 35, 29,10]);

			// Hole to secure the cylinder
			translate([-4, 12.5, 15])
			rotate(90, [0, 1, 0])
			cylinder(d = 4, h = 25, $fs = 0.5);
		}
	}
}


// Holes used to hold the case.
module hole_case()
{
	cube([3.5, 20, 5]);

	translate([1.75,20,5])
	rotate(90, [1,0,0])
	cylinder(d=3.5, h=20, $fs = .5);

	translate([1.75,20,0])
	rotate(90, [1,0,0])
	cylinder(d=3.5, h=20, $fs = .5);
}