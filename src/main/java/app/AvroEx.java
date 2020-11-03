/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import example.avro.User;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

public class AvroEx {
    public static void main(String ...a) throws IOException {
        User user1 = new User();
        user1.setName("Alyssa");
        user1.setFavoriteNumber(256);
        // Leave favorite color null

        // Alternate constructor
        User user2 = new User("Ben", 7, "red");

        // Construct via builder
        User user3 = User.newBuilder()
                     .setName("Charlie")
                     .setFavoriteColor("blue")
                     .setFavoriteNumber(null)
                     .build();

        // Serialize user1, user2 and user3 to disk

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        DatumWriter<User> userDatumWriter = new SpecificDatumWriter<User>(User.class);
        try ( DataFileWriter<User> dataFileWriter = new DataFileWriter<User>(userDatumWriter) ) {
            dataFileWriter.create(user1.getSchema(), bout);
            dataFileWriter.append(user1);
            dataFileWriter.append(user2);
            dataFileWriter.append(user3);
        }

        byte [] bytes = bout.toByteArray();
        System.out.println(bytes.length);

        // Deserialize Users from disk
        //ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());

        SeekableByteArrayInput seekByteIn = new SeekableByteArrayInput(bytes);

        DatumReader<User> userDatumReader = new SpecificDatumReader<User>(User.class);
        try ( DataFileReader<User> dataFileReader = new DataFileReader<User>(seekByteIn, userDatumReader) ) {
            User user = null;
            while (dataFileReader.hasNext()) {
                // Reuse user object by passing it to next(). This saves us from
                // allocating and garbage collecting many objects for files with
                // many items.
                user = dataFileReader.next(user);
                System.out.println(user);
            }
        }
    }
}

