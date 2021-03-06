module tb_gaussainAccel;
    reg clk;
    reg reset_n;
    reg [3:0] addr;
    reg rd_en;
    reg wr_en;
    reg [31:0] writedata;
    reg err;
    wire [31:0] readdata;


    gaussianAccel DUT(.clk(clk),
                      .reset_n(reset_n),
                      .addr(addr),
                      .rd_en(rd_en),
                      .wr_en(wr_en),
                      .writedata(writedata),
                      .readdata(readdata));

    // comparing readdata output to expected output
    task checkoutput;
		input [31:0] expected_output;
		begin
			if(readdata !== expected_output) begin
				$display("ERROR: output is %d, expected output is %d", readdata, expected_output);
				err = 1'b1;
			end
		end
	endtask

    // start clock cycle
	initial begin
		forever begin
			clk = 1'b0; #5;
			clk = 1'b1; #5;
		end
	end
    
    // start testing
    //
    // all readdata outputs need to be scaled down by a factor of 10^6

    initial begin
		err = 1'b0;
        #5
        
        // reset
        rd_en = 1'b0;
        wr_en = 1'b0;
        reset_n = 1'b0;
        #10;
        reset_n = 1'b1;

        //////////////////////////// TEST 1:    output = 5, 
        //
        // img_arr = np.array([
        //     [1, 2, 3],
        //     [4, 5, 6],
        //     [7, 8, 9]
        //     ])

        wr_en = 1'b1;       // input img array

        addr = 4'd1;
        writedata = 32'd1;
        #10
        addr = 4'd2;
        writedata = 32'd2;
        #10
        addr = 4'd3;
        writedata = 32'd3;
        #10
        addr = 4'd4;
        writedata = 32'd4;
        #10
        addr = 4'd5;
        writedata = 32'd5;
        #10
        addr = 4'd6;
        writedata = 32'd6;
        #10
        addr = 4'd7;
        writedata = 32'd7;
        #10
        addr = 4'd8;
        writedata = 32'd8;
        #10
        addr = 4'd9;
        writedata = 32'd9;
        #10
        wr_en = 1'b0;   // read output value
        rd_en = 1'b1;
        addr = 4'd0;
        #10;
        checkoutput(32'd_5_000_000);         // 5 


        ///////////////////////////////// TEST 2:    output = 14.1881 * 10^6,
        //
        // img_arr = np.array([
        //     [1, 2, 3],
        //     [4, 50, 6],
        //     [7, 8, 9]
        //     ])

        rd_en = 1'b0;
        wr_en = 1'b1;       // input img array

        addr = 4'd1;
        writedata = 32'd1;
        #10
        addr = 4'd2;
        writedata = 32'd2;
        #10
        addr = 4'd3;
        writedata = 32'd3;
        #10
        addr = 4'd4;
        writedata = 32'd4;
        #10
        addr = 4'd5;
        writedata = 32'd50;
        #10
        addr = 4'd6;
        writedata = 32'd6;
        #10
        addr = 4'd7;
        writedata = 32'd7;
        #10
        addr = 4'd8;
        writedata = 32'd8;
        #10
        addr = 4'd9;
        writedata = 32'd9;
        #10
        wr_en = 1'b0;   // read output value
        rd_en = 1'b1;
        addr = 4'd0;
        #10;
        checkoutput(32'd_14_188_100);

        ///////////////////////////////// TEST 3:    output = 255
        //
        // img_arr = np.array([
        //     [255, 255, 255],
        //     [255, 255, 255],
        //     [255, 255, 255]
        //     ])

        rd_en = 1'b0;
        wr_en = 1'b1;       // input img array

        addr = 4'd1;
        writedata = 32'd255;
        #10
        addr = 4'd2;
        writedata = 32'd255;
        #10
        addr = 4'd3;
        writedata = 32'd255;
        #10
        addr = 4'd4;
        writedata = 32'd255;
        #10
        addr = 4'd5;
        writedata = 32'd255;
        #10
        addr = 4'd6;
        writedata = 32'd255;
        #10
        addr = 4'd7;
        writedata = 32'd255;
        #10
        addr = 4'd8;
        writedata = 32'd255;
        #10
        addr = 4'd9;
        writedata = 32'd255;
        #10
        wr_en = 1'b0;   // read output value
        rd_en = 1'b1;
        addr = 4'd0;
        #10;
        checkoutput(32'd_255_000_000);

        ///////////////////////////////// TEST 4:    output = 105.267847
        //
        // img_arr = np.array([
        //     [15, 51, 100],
        //     [20, 241, 95],
        //     [152, 75, 82]
        //     ])

        rd_en = 1'b0;
        wr_en = 1'b1;       // input img array

        addr = 4'd1;
        writedata = 32'd15;
        #10
        addr = 4'd2;
        writedata = 32'd51;
        #10
        addr = 4'd3;
        writedata = 32'd100;
        #10
        addr = 4'd4;
        writedata = 32'd20;
        #10
        addr = 4'd5;
        writedata = 32'd241;
        #10
        addr = 4'd6;
        writedata = 32'd95;
        #10
        addr = 4'd7;
        writedata = 32'd152;
        #10
        addr = 4'd8;
        writedata = 32'd75;
        #10
        addr = 4'd9;
        writedata = 32'd82;
        #10
        wr_en = 1'b0;   // read output value
        rd_en = 1'b1;
        addr = 4'd0;
        #10;
        checkoutput(32'd_105_267_847);

        ///////////////////////////////// TEST 5:    output = 0
        //
        // img_arr = np.array([
        //     [0, 0, 0],
        //     [0, 0, 0],
        //     [0, 0, 0]
        //     ])

        rd_en = 1'b0;
        wr_en = 1'b1;       // input img array

        addr = 4'd1;
        writedata = 32'd0;
        #10
        addr = 4'd2;
        writedata = 32'd0;
        #10
        addr = 4'd3;
        writedata = 32'd0;
        #10
        addr = 4'd4;
        writedata = 32'd0;
        #10
        addr = 4'd5;
        writedata = 32'd0;
        #10
        addr = 4'd6;
        writedata = 32'd0;
        #10
        addr = 4'd7;
        writedata = 32'd0;
        #10
        addr = 4'd8;
        writedata = 32'd0;
        #10
        addr = 4'd9;
        writedata = 32'd0;
        #10
        wr_en = 1'b0;   // read output value
        rd_en = 1'b1;
        addr = 4'd0;
        #10;
        checkoutput(32'd_0);
      


        if(~err) $display("PASSED");
		else $display("FAILED");

		$stop;
	end
endmodule