include('./httpClient.js');
loadModule('/System/Resources');

var script = readFile(getFile('./openEclipse.js'));
var response = postClient('http://localhost:49152/scripts',script)
print(response.body)