/***************************************************************************
 *  Copyright (C) 2011 by H-Store Project                                  *
 *  Brown University                                                       *
 *  Massachusetts Institute of Technology                                  *
 *  Yale University                                                        *
 *                                                                         *
 *  http://hstore.cs.brown.edu/                                            *
 *                                                                         *
 *  Permission is hereby granted, free of charge, to any person obtaining  *
 *  a copy of this software and associated documentation files (the        *
 *  "Software"), to deal in the Software without restriction, including    *
 *  without limitation the rights to use, copy, modify, merge, publish,    *
 *  distribute, sublicense, and/or sell copies of the Software, and to     *
 *  permit persons to whom the Software is furnished to do so, subject to  *
 *  the following conditions:                                              *
 *                                                                         *
 *  The above copyright notice and this permission notice shall be         *
 *  included in all copies or substantial portions of the Software.        *
 *                                                                         *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        *
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF     *
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. *
 *  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR      *
 *  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,  *
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR  *
 *  OTHER DEALINGS IN THE SOFTWARE.                                        *
 ***************************************************************************/
package edu.brown.benchmark.seats.util;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.voltdb.catalog.Database;
import org.voltdb.types.TimestampType;

public class FlightId extends CompositeId {
    
    private static final int COMPOSITE_BITS[] = {
        16, // AIRLINE_ID
        16, // DEPART AIRPORT_ID
        16, // ARRIVE AIRPORT_ID
        16, // DEPART DATE
    };
    private static final long COMPOSITE_POWS[] = compositeBitsPreCompute(COMPOSITE_BITS);
    
    /** 
     * The airline for this flight
     */
    private int airline_id;
    /**
     * The id of the departure airport
     */
    private int depart_airport_id;
    /**
     * The id of the arrival airport
     */
    private int arrive_airport_id;
    /**
     * This is the departure time of the flight in minutes since the benchmark start date
     * @see SEATSBaseClient.getFlightTimeInMinutes()
     */
    private int depart_date;
    
    
    /**
     * Constructor
     * @param airline_id - The airline for this flight
     * @param depart_airport_id - the id of the departure airport
     * @param arrive_airport_id - the id of the arrival airport
     * @param benchmark_start - the base date of when the benchmark data starts (including past days)
     * @param flight_date - when departure date of the flight
     */
    public FlightId(long airline_id, long depart_airport_id, long arrive_airport_id, Timestamp benchmark_start, Timestamp flight_date) {
        this.airline_id = (int)airline_id;
        this.depart_airport_id = (int)depart_airport_id;
        this.arrive_airport_id = (int)arrive_airport_id;
        this.depart_date = FlightId.calculateFlightDate(benchmark_start, flight_date);
        assert(this.depart_date >= 0) : benchmark_start + " / " + flight_date;
    }
    
    public FlightId() {
        // Nothing!
    }
    
    /**
     * Constructor. Converts a composite id generated by encode() into the full object
     * @param composite_id
     */
    public FlightId(long composite_id) {
        this.set(composite_id);
    }
    
    public void set(long composite_id) {
        this.decode(composite_id);
    }
    
    @Override
    public long encode() {
        return (this.encode(COMPOSITE_BITS, COMPOSITE_POWS));
    }

    @Override
    public void decode(long composite_id) {
        long values[] = super.decode(composite_id, COMPOSITE_BITS, COMPOSITE_POWS);
        this.airline_id = (int)values[0];
        this.depart_airport_id = (int)values[1];
        this.arrive_airport_id = (int)values[2];
        this.depart_date = (int)values[3];
    }

    @Override
    public long[] toArray() {
        return (new long[]{ this.airline_id,
                            this.depart_airport_id,
                            this.arrive_airport_id,
                            this.depart_date });
    }
    
    /**
     * 
     * @param benchmark_start
     * @param flight_date
     * @return
     */
    protected static final long calculateFlightDate(TimestampType benchmark_start, TimestampType flight_date) {
        return (flight_date.getMSTime() - benchmark_start.getMSTime()) / 3600000; // 60s * 60m * 1000
    }
    
    /**
     * @return the id
     */
    public long getAirlineId() {
        return airline_id;
    }

    /**
     * @return the depart_airport_id
     */
    public long getDepartAirportId() {
        return depart_airport_id;
    }

    /**
     * @return the arrive_airport_id
     */
    public long getArriveAirportId() {
        return arrive_airport_id;
    }

    /**
     * @return the flight departure date
     */
    public TimestampType getDepartDate(TimestampType benchmark_start) {
        return (new TimestampType(benchmark_start.getTime() + (this.depart_date * SEATSConstants.MICROSECONDS_PER_MINUTE * 60)));
    }
    
    public boolean isUpcoming(Timestamp benchmark_start, long past_days) {
        Timestamp depart_date = this.getDepartDate(benchmark_start);
        return ((depart_date.getTime() - benchmark_start.getTime()) >= (past_days * SEATSConstants.MILLISECONDS_PER_DAY)); 
    }
    
    @Override
    public String toString() {
        return String.format("FlightId{airline=%d,depart=%d,arrive=%d,date=%s}",
                             this.airline_id, this.depart_airport_id, this.arrive_airport_id, this.depart_date);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FlightId) {
            FlightId o = (FlightId)obj;
            return (this.airline_id == o.airline_id &&
                    this.depart_airport_id == o.depart_airport_id &&
                    this.arrive_airport_id == o.arrive_airport_id &&
                    this.depart_date == o.depart_date);
        }
        return (false);
    }
}
