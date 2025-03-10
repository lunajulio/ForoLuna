const Sidebar = () => {
    return (
      <div className="sticky top-20 h-[calc(100vh-5rem)]">
        <div>
          <input
            type="search"
            placeholder="Search"
            className="w-full px-4 py-2 bg-gray-900 border border-gray-700 rounded-md text-white"
          />
        </div>
  
        <nav className="space-y-1 mt-4">
          <div className="text-gray-400  px-3 text-sm font-medium">MENU</div>
          <a href="#" className="flex items-center px-3 py-2 text-white hover:bg-gray-800 rounded-md">
            Questions
          </a>
          <a href="#" className="flex items-center px-3 py-2 text-gray-400 hover:bg-gray-800 rounded-md">
            Tags
          </a>
          <a href="#" className="flex items-center px-3 py-2 text-gray-400 hover:bg-gray-800 rounded-md">
            Ranking
          </a>
        </nav>
      </div>
    )
  }
  
  export default Sidebar