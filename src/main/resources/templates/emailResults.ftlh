<html>
<head>
<style>
  table, th, td {
      border: 1px solid black;
      border-collapse: collapse;
  }

  th, td {
      padding: 15px;
  }
  table td:nth-child(2) {
      text-align: end;
  }

  .upload_success {
      color: green;
  }

  .upload_failure {
      color: red;
  }

</style>
</head>
<body>
Status summary for eText Upload Processing (${env}) as of <#if env == 'CI'>RUN-TIMESTAMP<#else>${runTime?datetime}</#if>

<dl>
  <dt>Batch ID</dt>
  <dd>${batch.id}</dd>
  <dt>Uploader</dt>
  <dd>${batch.uploader!''}</dd>
  <dt>Date</dt>
  <dd><#if env == 'CI'>ROW-TIMESTAMP<#else>${batch.runDate?datetime}</#if></dd>
</dl>

Processed eTexts with the following ${batch.results?size} entries:
<table>
<thead>
<tr>
   <th>Filename</th>
   <th>Tool</th>
   <th>Tool ID</th>
   <th>SIS Course ID</th>
   <th>Canvas Course ID</th>
   <th>Deployment ID</th>
   <th>Message</th>
</tr>
</thead>
<tbody>
<#list batch.results as row>
    <tr>
        <td>${row.filename!''}</td>
        <td>${row.tool!''}</td>
        <td>${row.toolId!''}</td>
        <td>${row.sisCourseId!''}</td>
        <td>${row.canvasCourseId!''}</td>
        <td>${row.deploymentId!''}</td>
        <td>${row.message!''}</td>
    </tr>
</#list>
</tbody>
</table>
</body>
</html>