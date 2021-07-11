{% macro showTopNav() %}
<navbar type="dark">
  <a slot="brand" href="{{baseUrl}}/index.html" title="Home" class="navbar-brand"><img width="30px" src="{{baseUrl}}/favicon.ico"/></a>
  <li><a href="{{baseUrl}}/index.html" class="nav-link">HOME</a></li>
  <li><a href="{{baseUrl}}/showcase.html" class="nav-link">SHOWCASE</a></li>
  <li><a href="{{baseUrl}}/ug/index.html" class="nav-link">USER GUIDE</a></li>
  <li><a href="{{baseUrl}}/dg/index.html" class="nav-link">DEVELOPER GUIDE</a></li>
  <li><a href="{{baseUrl}}/about.html" class="nav-link">ABOUT</a></li>
  <li><a href="{{baseUrl}}/contact.html" class="nav-link">CONTACT</a></li>
  <li tags="dev"><a href="https://reposense.org" class="nav-link" target="_blank"><md>:fas-external-link-alt: PRODUCTION SITE</md></a></li>
  <li><a href="https://github.com/RepoSense/reposense" target="_blank" class="nav-link"><md>:fab-github:</md></a></li>
  <li slot="right">
    <form class="navbar-form">
      <searchbar :data="searchData" placeholder="Search" :on-hit="searchCallback" menu-align-right></searchbar>
    </form>
  </li>
</navbar>
{% endmacro %}
{{ showTopNav() }}
