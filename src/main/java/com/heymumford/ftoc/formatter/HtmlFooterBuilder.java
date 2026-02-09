package com.heymumford.ftoc.formatter;

/**
 * Builds the HTML footer section including closing tags and JavaScript functionality.
 * Extracted from TocFormatter.generateHtmlToc() for improved modularity.
 */
public class HtmlFooterBuilder {

    /**
     * Build the complete HTML footer section with JavaScript for collapsible sections and pagination.
     *
     * @param needsPagination Whether to include pagination JavaScript
     * @param totalPages Total number of pages (only used if needsPagination is true)
     * @return HTML footer with closing tags and JavaScript
     */
    public String build(boolean needsPagination, int totalPages) {
        StringBuilder footer = new StringBuilder();
        footer.append("  </div>\n"); // Close main-content

        // Add JavaScript for collapsible sections and pagination
        footer.append("<script>\n");
        footer.append(buildCollapsibleScript());

        if (needsPagination) {
            footer.append(buildPaginationScript(totalPages));
        }

        footer.append("});\n");
        footer.append("</script>\n");
        footer.append("</body>\n");
        footer.append("</html>");

        return footer.toString();
    }

    /**
     * Build JavaScript for collapsible sections.
     *
     * @return JavaScript code for collapsible functionality
     */
    private String buildCollapsibleScript() {
        StringBuilder script = new StringBuilder();
        script.append("document.addEventListener('DOMContentLoaded', function() {\n");
        script.append("  var coll = document.getElementsByClassName('collapsible');\n");
        script.append("  for (var i = 0; i < coll.length; i++) {\n");
        script.append("    coll[i].addEventListener('click', function() {\n");
        script.append("      this.classList.toggle('active');\n");
        script.append("      var content = this.nextElementSibling;\n");
        script.append("      if (content.style.maxHeight && content.style.maxHeight !== 'none') {\n");
        script.append("        content.style.maxHeight = null;\n");
        script.append("      } else {\n");
        script.append("        content.style.maxHeight = content.scrollHeight + 'px';\n");
        script.append("      }\n");
        script.append("    });\n");
        script.append("  }\n");
        return script.toString();
    }

    /**
     * Build JavaScript for pagination functionality.
     *
     * @param totalPages Total number of pages
     * @return JavaScript code for pagination
     */
    private String buildPaginationScript(int totalPages) {
        StringBuilder script = new StringBuilder();
        script.append("  // Pagination setup\n");
        script.append("  var totalPages = ").append(totalPages).append(";\n");
        script.append("  var currentPageNum = 1;\n");

        script.append("  function setupPagination() {\n");
        script.append("    var paginationTop = document.getElementById('pagination-top');\n");
        script.append("    var paginationBottom = document.getElementById('pagination-bottom');\n");
        script.append("    paginationTop.innerHTML = '';\n");
        script.append("    paginationBottom.innerHTML = '';\n");

        script.append("    // Previous button\n");
        script.append("    var prevButton = document.createElement('button');\n");
        script.append("    prevButton.innerHTML = '&laquo; Previous';\n");
        script.append("    prevButton.disabled = currentPageNum === 1;\n");
        script.append("    prevButton.addEventListener('click', function() { changePage(currentPageNum - 1); });\n");
        script.append("    paginationTop.appendChild(prevButton.cloneNode(true));\n");
        script.append("    paginationBottom.appendChild(prevButton);\n");

        script.append("    // Page buttons\n");
        script.append("    for (var i = 1; i <= totalPages; i++) {\n");
        script.append("      var pageButton = document.createElement('button');\n");
        script.append("      pageButton.textContent = i;\n");
        script.append("      pageButton.classList.toggle('current', i === currentPageNum);\n");
        script.append("      pageButton.dataset.page = i;\n");
        script.append("      pageButton.addEventListener('click', function() { changePage(parseInt(this.dataset.page)); });\n");
        script.append("      paginationTop.appendChild(pageButton.cloneNode(true));\n");
        script.append("      paginationBottom.appendChild(pageButton);\n");
        script.append("    }\n");

        script.append("    // Next button\n");
        script.append("    var nextButton = document.createElement('button');\n");
        script.append("    nextButton.innerHTML = 'Next &raquo;';\n");
        script.append("    nextButton.disabled = currentPageNum === totalPages;\n");
        script.append("    nextButton.addEventListener('click', function() { changePage(currentPageNum + 1); });\n");
        script.append("    paginationTop.appendChild(nextButton.cloneNode(true));\n");
        script.append("    paginationBottom.appendChild(nextButton);\n");
        script.append("  }\n");

        script.append("  function changePage(pageNum) {\n");
        script.append("    if (pageNum < 1 || pageNum > totalPages) return;\n");
        script.append("    var pages = document.getElementsByClassName('page');\n");
        script.append("    for (var i = 0; i < pages.length; i++) {\n");
        script.append("      pages[i].classList.remove('active');\n");
        script.append("    }\n");
        script.append("    document.getElementById('page-' + pageNum).classList.add('active');\n");
        script.append("    currentPageNum = pageNum;\n");
        script.append("    setupPagination();\n");
        script.append("    window.scrollTo(0, 0);\n");
        script.append("  }\n");

        script.append("  // Initialize pagination\n");
        script.append("  setupPagination();\n");

        return script.toString();
    }
}
