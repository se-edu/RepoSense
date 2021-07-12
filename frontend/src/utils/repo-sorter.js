function getTotalCommits(total, group) {
  return total + group.checkedFileTypeContribution;
}

function getGroupCommitsVariance(total, group) {
  return total + group.variance;
}

function sortingHelper(element, sortingOption) {
  if (sortingOption === 'totalCommits') {
    return element.reduce(getTotalCommits, 0);
  }
  if (sortingOption === 'variance') {
    return element.reduce(getGroupCommitsVariance, 0);
  }
  if (sortingOption === 'displayName') {
    return window.getAuthorDisplayName(element);
  }
  return element[0][sortingOption];
}

function groupByRepos(repos, sortingControl) {
  const sortedRepos = [];
  const {
    sortingWithinOption, sortingOption, isSortingDsc, isSortingWithinDsc,
  } = sortingControl;
  const sortWithinOption = sortingWithinOption === 'title' ? 'displayName' : sortingWithinOption;
  const sortOption = sortingOption === 'groupTitle' ? 'searchPath' : sortingOption;
  repos.forEach((users) => {
    if (sortWithinOption === 'totalCommits') {
      users.sort(window.comparator((ele) => ele.checkedFileTypeContribution));
    } else {
      users.sort(window.comparator((ele) => ele[sortWithinOption]));
    }

    if (isSortingWithinDsc) {
      users.reverse();
    }
    sortedRepos.push(users);
  });
  sortedRepos.sort(window.comparator(sortingHelper, sortOption));
  if (isSortingDsc) {
    sortedRepos.reverse();
  }
  return sortedRepos;
}

function groupByNone(repos, sortingControl) {
  const sortedRepos = [];
  const { sortingOption, isSortingDsc } = sortingControl;
  const isSortingGroupTitle = sortingOption === 'groupTitle';
  repos.forEach((users) => {
    users.forEach((user) => {
      sortedRepos.push(user);
    });
  });
  sortedRepos.sort(window.comparator((repo) => {
    if (isSortingGroupTitle) {
      return repo.searchPath + repo.name;
    }
    if (sortingOption === 'totalCommits') {
      return repo.checkedFileTypeContribution;
    }
    return repo[sortingOption];
  }));
  if (isSortingDsc) {
    sortedRepos.reverse();
  }

  return sortedRepos;
}

function groupByAuthors(repos, sortingControl) {
  const authorMap = {};
  const filtered = [];
  const {
    sortingWithinOption, sortingOption, isSortingDsc, isSortingWithinDsc,
  } = sortingControl;
  const sortWithinOption = sortingWithinOption === 'title' ? 'searchPath' : sortingWithinOption;
  const sortOption = sortingOption === 'groupTitle' ? 'displayName' : sortingOption;
  repos.forEach((users) => {
    users.forEach((user) => {
      if (Object.keys(authorMap).includes(user.name)) {
        authorMap[user.name].push(user);
      } else {
        authorMap[user.name] = [user];
      }
    });
  });
  Object.keys(authorMap).forEach((author) => {
    if (sortWithinOption === 'totalCommits') {
      authorMap[author].sort(window.comparator((repo) => repo.checkedFileTypeContribution));
    } else {
      authorMap[author].sort(window.comparator((repo) => repo[sortWithinOption]));
    }
    if (isSortingWithinDsc) {
      authorMap[author].reverse();
    }
    filtered.push(authorMap[author]);
  });

  filtered.sort(window.comparator(sortingHelper, sortOption));
  if (isSortingDsc) {
    filtered.reverse();
  }
  return filtered;
}

function sortFiltered(filtered, filterControl) {
  const { filterGroupSelection } = filterControl;
  let full = [];

  if (filterGroupSelection === 'groupByNone') {
    // push all repos into the same group
    full[0] = groupByNone(filtered, filterControl);
  } else if (filterGroupSelection === 'groupByAuthors') {
    full = groupByAuthors(filtered, filterControl);
  } else {
    full = groupByRepos(filtered, filterControl);
  }

  return full;
}

export default sortFiltered;
