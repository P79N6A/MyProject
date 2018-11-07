/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations
 * under the License.
*/

//include 'fb303.thrift'

struct Plan
{
    1: string        plan,      // 执行计划
    2: i64           timestamp, // 时间戳
}
/*
struct Result
{
    1: i32      result_code,    // 执行结果
    2: string   result_info,    //  
}
*/
service ETLWrapper
{
    /*  
    Result doPlan(1: Plan info)
    Result getPlan(1: Plan info)
    */
    void doPlan(1: Plan info)
    void getPlan(1: Plan info)
    oneway void doPlan_async(1: Plan info)
    oneway void stopPlan_async(1: Plan info)
}
