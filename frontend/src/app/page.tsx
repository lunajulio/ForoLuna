import Nav from '../components/Nav'
export default function Home() {
  return (
    <div>
      <Nav />
      <main className="flex min-h-screen flex-col items-center justify-center">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-white mb-4">
          Luna Community
        </h1>
        <p className="text-xl text-gray-300 mb-8">
          Welcome to the developers community!
        </p>
      </div>
    </main>
    </div>
  );
}