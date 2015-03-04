include <./servo.scad>
include <./pcb.scad>
include <./battery_holder_3.scad>
include <./battery_holder_single.scad>


module electronics()
{
	
	translate([-10.5, 60.3, 8.5])
	servo();


	translate([-7, 100, 9.9])
	{
		battery_3();

		translate([46, 59, 0])
		rotate(90, [0, 0, 1])
		battery_single();
	}

	translate([-37, 58, 5.5])
	pcb();
}
