package com.example.team21_zooseeker.helpers;

import android.content.Context;
import android.util.Log;

import com.example.team21_zooseeker.activities.route.IdentifiedWeightedEdge;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.nio.json.JSONImporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ZooData {
    public static class VertexInfo {
        public static enum Kind {
            // The SerializedName annotation tells GSON how to convert
            // from the strings in our JSON to this Enum.
            @SerializedName("gate") GATE,

            @SerializedName("exhibit") EXHIBIT,

            @SerializedName("intersection") INTERSECTION,

            @SerializedName("exhibit_group") EXHIBIT_GROUP
        }

        public String id;
        public String group_id;
        public Kind kind;
        public String name;
        public List<String> tags;

        // Convert to Latlng later
        public String lat;
        public String lng;

        @Override
        public String toString () {
            return "\nVertexInfo{" +
                    "id='" + id + '\'' +
                    ", kind=" + kind +
                    ", name='" + name + '\'' +
                    ", tags=" + tags + '\'' +
                    ", parent_id=" + group_id + '\'' +
                    ", lat=" + lat + '\''+
                    ", lng=" + lng +
                    '}';
        }

        public String getName () {
            return name;
        }

        public String getTag () {
            String tagConnected = "";

            for (String tag: tags) {
                tagConnected +=tag+ ",";
            }
            return tagConnected;
        }


    }

    public static class EdgeInfo {
        public String id;
        public String street;
    }

    public static Map<String, VertexInfo> loadVertexInfoJSON(Context context, String path) {
        try {
            InputStream inputStream = context.getAssets().open(path);
            Reader reader = new InputStreamReader(inputStream);

            Gson gson = new Gson();
            Type type = new TypeToken<List<ZooData.VertexInfo>>() {
            }.getType();
            List<ZooData.VertexInfo> zooData = gson.fromJson(reader, type);

            // Added 5/21/2022 to change lat and lng of exhibits with parent id
            List<ZooData.VertexInfo> parentIdValues = zooData.stream().
                    filter(node -> node.kind == VertexInfo.Kind.EXHIBIT_GROUP).
                    collect(Collectors.toList());

            // changes all nodes with parent_id to lat and lng of that Exhibit Group
            // e.g. Dove
            zooData.forEach(node -> {
                parentIdValues.stream().forEach(parent -> {
                    if (parent.id.equals(node.group_id)) {
                        node.lng = parent.lng;
                        node.lat = parent.lat;
                        Log.d("Parent Id Animal", node.toString());
                    }
                });
            });

            // This code is equivalent to:
            //
            // Map<String, ZooData.VertexInfo> indexedZooData = new HashMap();
            // for (ZooData.VertexInfo datum : zooData) {
            //   indexedZooData[datum.id] = datum;
            // }
            //
            Map<String, ZooData.VertexInfo> indexedZooData = zooData
                    .stream()
                    .collect(Collectors.toMap(v -> v.id, datum -> datum));

            return indexedZooData;
        }
            catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public static Map<String, ZooData.EdgeInfo> loadEdgeInfoJSON(Context context, String path) {
        try {
            InputStream inputStream = context.getAssets().open(path);
            Reader reader = new InputStreamReader(inputStream);

            Gson gson = new Gson();
            Type type = new TypeToken<List<ZooData.EdgeInfo>>() {
            }.getType();
            List<ZooData.EdgeInfo> zooData = gson.fromJson(reader, type);

            Map<String, ZooData.EdgeInfo> indexedZooData = zooData
                    .stream()
                    .collect(Collectors.toMap(v -> v.id, datum -> datum));

            return indexedZooData;
        }
        catch(IOException e){
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public static Graph<String, IdentifiedWeightedEdge> loadZooGraphJSON(Context context, String path) {
        // Create an empty graph to populate.
        Graph<String, IdentifiedWeightedEdge> g = new DefaultUndirectedWeightedGraph<>(IdentifiedWeightedEdge.class);

        // Create an importer that can be used to populate our empty graph.
        JSONImporter<String, IdentifiedWeightedEdge> importer = new JSONImporter<>();

        // We don't need to convert the vertices in the graph, so we return them as is.
        importer.setVertexFactory(v -> v);

        // We need to make sure we set the IDs on our edges from the 'id' attribute.
        // While this is automatic for vertices, it isn't for edges. We keep the
        // definition of this in the IdentifiedWeightedEdge class for convenience.
        importer.addEdgeAttributeConsumer(IdentifiedWeightedEdge::attributeConsumer);

        // On Android, you would use context.getAssets().open(path) here like in Lab 5.
        try {
            InputStream inputStream = context.getAssets().open(path);
            Reader reader = new InputStreamReader(inputStream);

            // And now we just import it!
            importer.importGraph(g, reader);

            return g;
        }
        catch(IOException e){
            e.printStackTrace();
            return null;
        }

    }
}
