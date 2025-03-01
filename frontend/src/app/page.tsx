import Image from "next/image";
import Nav from "../components/Nav";
import RegisterForm from "../components/RegisterForm";

export default function Home() {
  return (
    <div className="bg-black min-h-screen">
      <Nav/>
      <RegisterForm/>
    </div>
  );
}
