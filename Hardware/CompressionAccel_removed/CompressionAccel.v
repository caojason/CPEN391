module CompressionAccel (
    input clk,

    // slave (CPU-facing)
    input reset_n,
    input addr,
    input rd_en,
    input wr_en,
    output reg [31:0] readdata,
    input [31:0] writedata

    // master (SDRAM-facing)
    input reg master_waitrequest,
    input reg [31:0] master_readdata,
    input reg master_readdatavalid,
    output reg [31:0] master_address,
    output reg master_read,
    output reg master_write, 
    output reg [31:0] master_writedata
);

    enum {RESET,
            // read and write to sdram
            WAIT_FOR_MASTER,
            READ_COLOUR0,
            UPDATE_COLOUR0,
            READ_COLOUR1,
            UPDATE_COLOUR1,
            READ_COLOUR2,
            UPDATE_COLOUR3,
            READ_NEWCOLOUR,
            UPDATE_NEWCOLOUR,
            WRITE_OUTPUT,
            WAIT_FOR_WRITE,

            }curr_state;
    
    reg [31:0] colour0, colour1, colour2, newColour;

    always @(posedge clk) begin
        if (reset_n == 0) begin // synchronous reset
            slave_waitrequest = 1'b1;
        end else if (wr_en) begin
            case(curr_state) begin
                RESET: begin
                    curr_state = RESET;

                end
                // SDRAM
                WAIT_FOR_MASTER: begin
                    slave_waitrequest = 1'b1;
                    curr_state = READ_COLOUR;
                end
                READ_COLOUR: begin
                    curr_state = master_waitrequest ? READ_NEWCOLOUR : UPDATE_NEWCOLOUR;
                end
                UPDATE_NEWCOLOUR: begin
                    newColour = (!master_waitrequest & master_readdatavalid) ? master_readdata : newColour;
                    curr_state = (!master_waitrequest & master_readdatavalid) ? WAIT_UPDATE1 : UPDATE_NEWCOLOUR;
                end
                WAIT_UPDATE1: begin
                    curr_state = READ_COLOUR;
                end
            endcase
        end
    end

    always @(*) begin
        master_address = 32'b0; 
        master_read = 1'b0; 
        master_write = 1'b0; 
        master_writedata = 32'b0;


        case(curr_state) begin
            RESET: begin
                readdata = 32'b0;
            end
        endcase
    end

endmodule
