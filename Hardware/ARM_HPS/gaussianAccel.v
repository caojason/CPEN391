`define K00     32'd75114       // Gaussian kernel values of K[i][j] * 10^6
`define K01     32'd123841
`define K02     32'd75114
`define K10     32'd123841
`define K11     32'd204180
`define K12     32'd123841
`define K20     32'd75114
`define K21     32'd123841
`define K22     32'd75114

module gaussianAccel (
    input clk,
    input reset_n,
    input [3:0] addr,
    input rd_en,
    input wr_en,
    output reg [31:0] readdata,
    input [31:0] writedata
);
    reg [31:0] img00, img01, img02, img10, img11, img12, img20, img21, img22; 

    wire [31:0] test;

    // writing
    always@(posedge clk) begin
        if (reset_n == 0) begin // synchronous reset
            img00 = 32'b0; // reset img pixel data
            img01 = 32'b0;
            img02 = 32'b0;
            img10 = 32'b0;
            img11 = 32'b0;
            img12 = 32'b0;
            img20 = 32'b0;
            img21 = 32'b0;
            img22 = 32'b0;
        end else if (wr_en == 1 && addr == 4'd1) // write img data to correct location
            img00 = writedata;
        else if (wr_en == 1 && addr == 4'd2)
            img01 = writedata;
        else if (wr_en == 1 && addr == 4'd3)
            img02 = writedata;
        else if (wr_en == 1 && addr == 4'd4)
            img10 = writedata;
        else if (wr_en == 1 && addr == 4'd5)
            img11 = writedata;
        else if (wr_en == 1 && addr == 4'd6)
            img12 = writedata;
        else if (wr_en == 1 && addr == 4'd7)
            img20 = writedata;
        else if (wr_en == 1 && addr == 4'd8)
            img21 = writedata;
        else if (wr_en == 1 && addr == 4'd9)
            img22 = writedata;
    end

    // reading
    always@(*) begin
        if (rd_en == 1) begin
            if (addr == 4'd0) begin // if reading from address 0, get convolution of kernel with 3x3 img
                readdata = (img00*`K00 + img01*`K01 + img02*`K02 + img10*`K10 + img11*`K11 + img12*`K12 + img20*`K20 + img21*`K21 + img22*`K22);
            end
        end else begin
            readdata = 32'd0; // default value is 0
        end
    end
endmodule