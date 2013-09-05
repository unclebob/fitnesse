function doTogglePassedTestsInHistory()
{
  index = $("tr>th[colspan]").index()+1; // possition of the most recent history result
  $('td:nth-child('+index+')').filter('.pass').parent().toggle();
}
function doTogglePassedTestsInSuite()
{
  $('tr.pass').toggle();
}
