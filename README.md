# San Diego ZooSeeker
A companion app for the San Diego Zoo that lets you select different exhibits and plan a customized itinerary for your trip using real-time location detection.

<h2>Features</h2>

<h3>Selecting different animal exhibits</h3>
A search bar is implemented to facilitate the selection process, equipped with both autocomplete and speech-to-text features. The user can type in/input whatever exhibit they want to see and can select the corresponding dropdown menu item to add it to their list. The user can also edit/clear their selection list. These CRUD actions are implemented behind the scenes using the <i><a href="https://developer.android.com/jetpack/androidx/releases/room">Room</a></i> database.

<h3>Planning the route</h3>
Once the user finishes selecting their desired exhibits and hits the plan button, a shortest path algorithm (modified Dijkstra) is run on a graph that represents the entire zoo. It calculates the most efficient route that hits all the selected exhibits and returns a list. The list is then displayed as a preview and is ordered from nearest to farthest.

<h3>Detailed Directions</h3>
After the user confirms that their plan looks ideal, a carousel is quickly made behind the scenes to display directions, each card for one exhibit. The app also utilizes live user location using the <a href="https://developers.google.com/maps/documentation/android-sdk/overview">Maps API</a> to display accurate directions to the respective exhibit. Another feature that we implemented was off track suggestions; this accounts for the case where if the user goes too far away from the next exhibit to the point where there's a different exhibit that's closer, the app prompts for a route replan. In addition to that, there is also a skip feature where the user can directly skip an exhibit in the middle of the trip if they feel like it.

<h3>Miscellaneous</h3>
Some other minor features include app persistence, toggling between brief and detailed directions, and including entrance/exit gate automatically during route calculations.

<h2>Planning documents, Big board and other documentation</h2>

1. <a href="https://docs.google.com/document/d/1c-P80venABliv92ZEvaFhgadUSvH1Lb-RO09qMZrYO8/edit?usp=sharing" target="_blank" rel="noopener">Milestone 1 Planning</a>
2. <a href="https://docs.google.com/document/d/1ThV8yjH-tTbN12c9IOMTKEmbTOn9t_3YGecAp-JBPNk/edit?usp=sharing" target="_blank" rel="noopener">Milestone 2 Planning</a>
3. <a href="https://app.zenhub.com/workspaces/cse-110-team-21-625e68c35dbe5f001c7cfdb5/board" target="_blank" rel="noopener">Zenhub Board</a>
4. <a href="https://sites.google.com/eng.ucsd.edu/cse110spring2022/home?authuser=0" target="_blank" rel="noopener">Course Website</a>

<h2>Collaborators:</h2>

1. Kinan Alatasi
2. Ari Winograd
3. Andrew Nguyen
4. Wesley Kiang
5. Elijah Pichler

<h2>Notes</h2>
This is a project done as part of the Spring 2022 CSE 110 course curriculum at the University of California, San Diego, and is accomplished with the help of the San Diego Zoo.
