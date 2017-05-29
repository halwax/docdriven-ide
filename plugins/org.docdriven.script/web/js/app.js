new Vue({
  el: '#app',
  data: {
    scripts: [
      {
        title: 'JS Script'
      }
    ]
  },
  mounted: function () {

    var appInstance = this;

    var scriptEditorDiv = this.$el.querySelector('#scriptEditor');
    var resultEditorDiv = this.$el.querySelector('#resultEditor');

    require.config({ paths: { 'vs': './vs' } });
    require(['vs/editor/editor.main'], function () {
      appInstance.scriptEditor = monaco.editor.create(scriptEditorDiv, {
        value: [
          'return {};'
        ].join('\n'),
        language: 'javascript'
      });
      appInstance.resultEditor = monaco.editor.create(resultEditorDiv, {
        value: [
          '{}'
        ].join('\n'),
        language: 'json'
      });
      appInstance.layoutEditors();
      window.addEventListener('resize', appInstance.layoutEditors);
      appInstance.layoutEditors();
    });
  },
  destroyed: function () {
    if (typeof this.scriptEditor !== 'undefined') {
      window.removeEventListener('resize', this.layoutEditors);
      this.scriptEditor.dispose();
    }
    if (typeof this.resultEditor !== 'undefined') {
      window.removeEventListener('resize', this.layoutEditors);
      this.resultEditor.dispose();
    }
  },
  methods: {
    layoutEditors: function () {
      this.scriptEditor.layout();
      this.resultEditor.layout();
    },
    runScript: function () {
      if (typeof this.scriptEditor !== 'undefined') {
        
        var appInstance = this;

        var script = this.scriptEditor.getValue();
        console.log(script);

        var request = new XMLHttpRequest();
        request.open('POST', '../scripts');
        request.setRequestHeader('content-type','application/javascript');
        request.addEventListener('load', function (event) {
          if (request.status >= 200 && request.status < 300) {
            var resultObj = JSON.parse(request.responseText);
            var prettyResultJSON = JSON.stringify(resultObj, null, 2);
            appInstance.resultEditor.setValue(prettyResultJSON);
          } else {
            appInstance.resultEditor.setValue(request.responseText);
          }
        });
        request.send(script);
      }
    }
  }
});