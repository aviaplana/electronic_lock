/*
	Top part of the stand.
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/

module lock_stand_bottom() 
{
	union()
	{
	
		// The bar that is going to be inserted in the top part.
		difference()
		{
			translate([0, 94.1, 0])
			cube([18, 100, 30]);

			translate([3, 94, 3])
			cube([12, 105, 24]);
		}

// START Arm bolt

		difference()
		{
			translate([7, 173.6, 42])
			cube([10, 3, 16]);

			translate([10.5, 163.6, 50])
			hole_case();
		}

		translate([7, 174.4, 42])
		cube([10, 20, 3]);

// END Arm bolt
		

// START Pilars

		translate([46, 173.5, 76])
		cube([4, 21, 4]);
		
		translate([-30, 173.5, 76])
		cube([4, 21, 4]);

//END Pilars


// START Base
		/*
			This is the base of the stand. 
			The holes are made to save printing timme.
		*/
		difference()
		{
			translate([-35, 193.7, -15])
			cube([90, 3, 150]);

			translate([0, 188, 0])
			{
				translate([3, 0, 3])
				cube([12, 10, 24]);

				translate([45, 10, 15])
				rotate(90, [1, 0, 0])
				linear_extrude(height = 6)
				polygon( points = [
					[0, 25],
					[5, 25],
					[5, -25],
					[0, -25],
					[-25, 0]
				]);

				translate([-25, 10, 15])
				rotate(90, [1, 0, 0])
				rotate(180, [0, 0, 1])
				linear_extrude(height = 6)
				polygon( points = [
					[0, 25],
					[5, 25],
					[5, -30],
					[-23, 0]
				]);

				translate([9, 10, -10])
				rotate(90, [1, 0, 0])
				rotate(-90, [0, 0, 1])
				linear_extrude(height = 6)
				polygon( points = [
					[0, 15],
					[0, -15],
					[-10, -5],
					[-10, 5]
				]);

				translate([2, 10, 56])
				rotate(90, [1, 0, 0])
				rotate(180, [0, 0, 1])
				linear_extrude(height = 6)
				polygon( points = [
					[0, 24],
					[0, -24],
					[28, -24],
					[28, -20],
					[32, -20],
					[32, -8]
				]);

				translate([18, 5, 56])
				rotate(-90, [1, 0, 0])
				linear_extrude(height = 6)
				polygon( points = [
					[0, 24],
					[0, -24],
					[28, -24],
					[28, -20],
					[32, -20],
					[32, -8]
				]);

				translate([-30, 10, 115])
				rotate(90, [1, 0, 0])
				rotate(180, [0, 0, 1])
				linear_extrude(height = 6)
				polygon( points = [
					[0, 15],
					[0, -15],
					[-32, -15]
				]);

				translate([2, 10, 103])
				rotate(90, [1, 0, 0])
				linear_extrude(height = 6)
				polygon( points = [
					[0, 15],
					[0, -15],
					[-30, -15]
				]);


				translate([50, 10, 103])
				rotate(90, [1, 0, 0])
				linear_extrude(height = 6)
				polygon( points = [
					[-32, 15],
					[0, -15],	
					[-32, -15]
				]);

				translate([50, 10, 115])
				rotate(90, [1, 0, 0])
				linear_extrude(height = 6)
				polygon( points = [
					[0, 15],
					[0, -15],
					[-32, 15]
				]);
			}
		}

// END Base

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