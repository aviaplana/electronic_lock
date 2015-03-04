/*
	The cylinder and servo gears.
	I'm using the MCAD library ( https://github.com/openscad/MCAD ).
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/

use <../libraries/MCAD/involute_gears.scad>


module gears() 
{
	// Teeth of the cylinder gear
	gear1_teeth = 25;
	
	// Teeth of the servo gear
	gear2_teeth = 7;

	axis_angle = 90;
	outside_circular_pitch = 450;

	outside_pitch_radius1 = gear1_teeth * outside_circular_pitch / 360;
	outside_pitch_radius2 = gear2_teeth * outside_circular_pitch / 360;
	
	pitch_apex1=outside_pitch_radius2 * sin (axis_angle) + 
		(outside_pitch_radius2 * cos (axis_angle) + outside_pitch_radius1) / tan (axis_angle);
	cone_distance = sqrt (pow (pitch_apex1, 2) + pow (outside_pitch_radius1, 2));
	pitch_apex2 = sqrt (pow (cone_distance, 2) - pow (outside_pitch_radius2, 2));

	pitch_angle1 = asin (outside_pitch_radius1 / cone_distance);
	pitch_angle2 = asin (outside_pitch_radius2 / cone_distance);


	rotate([0,0,90])
	translate ([0,0,pitch_apex1+9.6])
	{
		translate([0,0,-pitch_apex1])
		{

// START Cylinder gear
			difference()
			{
				bevel_gear (
					number_of_teeth=gear1_teeth,
					cone_distance=cone_distance,
					pressure_angle=30,
					outside_circular_pitch=outside_circular_pitch,
					gear_thickness=10
				);

// START Hole secure
/* 
	This hole is going to be used to secure the gear with the cylinder.
*/
				translate([0, 0, -8.5])
				{
					union()
					{
						rotate(90, [0, 1, 0])
						cylinder(d1=2.2, d2=2.2, h=50, $fs = 0.5, center=true);
						rotate(90, [0, 1, 0])
						translate([0, 0, 27.5]) 
						cylinder(d1=7, d2=7, h=7, $fs = 0.5, center=true);

						rotate(90, [0, 1, 0])
						translate([0, 0, -27.5])
						cylinder(d1=7, d2=7, h=7, $fs = 0.5, center=true);
					}
				};

// END Hole secure
		
			// Center hole, designed to fit in the lock.
			translate([0, 0, -13])
			cylinder(d1=12.7, d2=12.7, h=15.7);

// START Magnet holes

			translate([0,27,-9])
			cube([2,3,2]);

			rotate(-45,[0,0,1])
			translate([0,27,-9])
			cube([2,3,2]);

			rotate(45,[0,0,1])
			translate([0,27,-9])
			cube([2,3,2]);

// END Magnet holes

			}
// END Cylinder gear
		}

// START Servo gear
		union() 
		{
			rotate([0,-(pitch_angle1+pitch_angle2),0])
			translate([0,0,-pitch_apex2])
			bevel_gear (
				number_of_teeth=gear2_teeth,
				cone_distance=cone_distance,
				pressure_angle=30,
				outside_circular_pitch=outside_circular_pitch,
				bore_diameter=0
			);


			rotate(90, [0, 1, 0])
			translate([0, 0, 30]) 
			{
				// The axe that is going to connect the gear with the join
				cylinder(d=11.5, h=15);
				
				translate([0, 0, 15])
				difference()
				{
					cylinder(d=24, h=8);

					translate([0,0,5.9])
					{
						// This is for fitting the servo join with the gear
						cylinder(d=21.9, h=3, $fs = 0.5);

// START Holes for the bolts
						translate([8.05, 0, -6])
						cylinder(d=1.6, h=8, $fs = 0.5);

						translate([-8.05, 0, -6])
						cylinder(d=1.6, h=8, $fs = 0.5);

						translate([0, -8.05, -6])
						cylinder(d=1.6, h=8, $fs = 0.5);

						translate([0, 8.05, -6])
						cylinder(d=1.6, h=8, $fs = 0.5);
// END Holes for the bolts
					}
				}
			}

		}
// END Servo gear

	}
}
