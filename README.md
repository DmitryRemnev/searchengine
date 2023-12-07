# Site Search Engine
A search engine that helps site visitors quickly find the information they want by using the search box.
# Screenshots
![Dashboard](https://user-images.githubusercontent.com/87513274/139628698-caec41c7-1edf-4857-be5f-2d12adf76406.PNG)
![Management](https://user-images.githubusercontent.com/87513274/139628891-0424c020-6b34-4adb-8c0b-76d6ab31fcdf.PNG)
![Search](https://user-images.githubusercontent.com/87513274/139628957-71fef9ef-29f0-4807-b562-52d1f3112081.PNG)
# Principle of operation
1. In the configuration file before starting the application, you set the addresses of the sites that the engine searches.
2. The search engine itself bypasses all the pages of a given site and indexes them in such a way that then for any search query to find the most relevant (suitable) pages.
3. A user sends a request through the engine API. The query is a set of words used to find the pages of the site.
4. The query is transformed in a certain way into a list of words translated into a basic form.
5. The application searches the index of those pages in which all of these words occur.
6. The search results are ranked, sorted and given to the user.
